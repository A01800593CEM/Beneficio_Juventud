import { forwardRef, Module } from '@nestjs/common';
import { BranchService } from './branch.service';
import { BranchController } from './branch.controller';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Branch } from './entities/branch.entity';
import { CollaboratorsModule } from 'src/collaborators/collaborators.module';

@Module({
  controllers: [BranchController],
  providers: [BranchService],
  imports: [
    TypeOrmModule.forFeature([Branch]),
    forwardRef(()=> CollaboratorsModule)
  ],
  exports: [BranchModule]
})
export class BranchModule {}
