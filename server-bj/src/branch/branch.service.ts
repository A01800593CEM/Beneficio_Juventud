import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateBranchDto } from './dto/create-branch.dto';
import { UpdateBranchDto } from './dto/update-branch.dto';
import { InjectRepository } from '@nestjs/typeorm';
import { Branch } from './entities/branch.entity';
import { Repository } from 'typeorm';

@Injectable()
export class BranchService {
  constructor(
    @InjectRepository(Branch)
    private branchRepository: Repository<Branch>,
  ) {}
  async create(createBranchDto: CreateBranchDto): Promise<Branch>{
    const branch = this.branchRepository.create(createBranchDto)
    return this.branchRepository.save(branch);
  }

  async findAll(): Promise<Branch[]> {
    return this.branchRepository.find({relations: ['collaborator']});
  }

  async findOne(id: number): Promise<Branch | null> {
    return this.branchRepository.findOne({
      where: {branchId: id},
      relations: ['collaborator'],
    });
  }

  async update(id: number, updateBranchDto: UpdateBranchDto): Promise<Branch | null> {
    const branch = await this.branchRepository.preload({
      branchId: id,
      ...updateBranchDto
    })
    if (!branch){
      throw new NotFoundException(`Branch with id ${id} not found`)
    }
    return this.branchRepository.save(branch);
  }

  async remove(id: number): Promise<void>{
    await this.branchRepository.delete(id);
  }
}
